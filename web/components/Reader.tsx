import clsx from 'clsx'
import { notFound } from 'next/navigation'
import { JSX } from 'react'
import { assertUnreachable } from '@/lib/assert'
import { db, sql } from '@/lib/db'
import { styled } from '@/lib/styled'
import {
	ChildNode,
	Node,
	Table,
	TableCell,
	TableHeading,
} from '@/lib/types/usfm'
import { TEST_PASSAGE } from './Reader.spec'

const Paragraph = styled.p('leading-loose')
const Quote = styled.p('leading-loose')

type ReaderProps = {
	passageRef: string
}

export async function Reader({ passageRef }: ReaderProps) {
	const passage = await getPassage(passageRef)

	return (
		<div className="text-gray-900 dark:text-gray-200 font-sans text-lg">
			{passage.map((node, index) => (
				<ReaderNode key={index} node={node} />
			))}
		</div>
	)
}

function ReaderNode({ node }: { node: Node }) {
	const type = node[0]

	switch (type) {
		case 'cl': {
			return (
				<h2 className="mb-10 first:mt-0 mt-20 font-bold flex flex-col items-center">
					<span className="block text-lg text-zinc-600 dark:text-zinc-400 mb-2">
						{node[1]}
					</span>
					<span className="block text-7xl">{node[2]}</span>
				</h2>
			)
		}

		case 's': {
			const Component = `h${node[1] + 2}` as keyof JSX.IntrinsicElements
			return (
				<Component className="text-2xl mb-4 mt-8 font-bold">
					{node[2]}
				</Component>
			)
		}

		case 'ms':
			return <h3 className="text-2xl mb-4 mt-8 font-bold">{node[1]}</h3>

		case 'iex':
			return (
				<Paragraph className="text-base italic mb-2 text-zinc-600 dark:text-zinc-400">
					{node[1]}
				</Paragraph>
			)

		case 'd':
			return <Paragraph className="italic mb-4">{node[1]}</Paragraph>

		case 'sp':
			return <Paragraph className="italic mt-4">{node[1]}</Paragraph>

		case 'p':
		case 'nb':
			return (
				<Paragraph className="mb-2 indent-2">
					{renderChildren(node[1])}
				</Paragraph>
			)

		case 'm':
			return <Paragraph className="mb-2">{renderChildren(node[1])}</Paragraph>

		case 'pm':
			return (
				<Paragraph className="mb-2 indent-2 ml-2">
					{renderChildren(node[1])}
				</Paragraph>
			)

		case 'pmo':
		case 'pmc':
		case 'pmr':
			return (
				<Paragraph className="mb-2 indent-2 ml-2">
					{renderChildren(node[1])}
				</Paragraph>
			)

		case 'pc':
			return (
				<Paragraph className="text-center">{renderChildren(node[1])}</Paragraph>
			)

		case 'pr':
		case 'cls':
			return (
				<Paragraph className="text-right">{renderChildren(node[1])}</Paragraph>
			)

		case 'q': {
			const level = node[1]
			return (
				<Quote
					// TODO: Hanging indent
					className={clsx(
						level === 2 && 'ml-2',
						level === 3 && 'ml-4',
						level === 4 && 'ml-6',
					)}
				>
					{renderChildren(node[2])}
				</Quote>
			)
		}

		case 'qa':
			return <Quote className="text-center italic">{node[1]}</Quote>

		case 'qr':
			return <Quote className="text-right">{renderChildren(node[1])}</Quote>

		case 'qc':
			return <Quote className="text-center">{renderChildren(node[1])}</Quote>

		// TODO
		case 'pi':
		case 'mi':
		case 'qm':
		case 'li':
		case 'lim':
			return <Quote>{renderChildren(node[2])}</Quote>

		case 'b':
			return <div className="h-4" />

		case 'table':
			return <ReaderTable node={node} />

		default:
			assertUnreachable(node)
	}
}

function ReaderChildNode({ node }: { node: ChildNode }) {
	const [type, value] = node

	switch (type) {
		case 'v':
			return (
				<span className="text-gray-500 relative align-super top-0.5 text-xs">
					{value}{' '}
				</span>
			)

		case 'qs':
			return <span className="italic float-right">{value}</span>

		case 'litl':
			return <span className="float-right">{value}</span>

		case 'wj':
			return <span className="text-red-600 dark:text-red-500">{value}</span>

		case 'em':
		case 'bd':
			return <span className="font-bold">{value}</span>

		case 'bk':
		case 'qt':
		case 'sig':
		case 'sls':
		case 'tl':
		case 'it':
			return <span className="italic">{value}</span>

		case 'nd':
		case 'sc':
			return <span className="[font-variant:small-caps]">{value}</span>

		case 'sup':
			return <span className="align-super text-sm">{value}</span>

		case 't':
			return <span>{value}</span>

		default:
			assertUnreachable(node)
	}
}

function renderChildren(nodes: ChildNode[]) {
	return nodes.map((node, index) => <ReaderChildNode key={index} node={node} />)
}

function ReaderTable({ node }: { node: Table }) {
	const { bodyRows, headerRows } = node[1].reduce(
		(acc, row) => {
			if (row.every((cell) => cell[0] === 'th')) {
				acc.headerRows.push(row as TableHeading[])
			} else {
				acc.bodyRows.push(row as TableCell[])
			}

			return acc
		},
		{
			bodyRows: [] as TableCell[][],
			headerRows: [] as TableHeading[][],
		},
	)

	return (
		<table>
			{headerRows ? (
				<thead>
					{headerRows.map((row, index) => (
						<tr key={index}>
							{row.map((cell, index) => (
								<th key={index}>{renderChildren(cell[1])}</th>
							))}
						</tr>
					))}
				</thead>
			) : null}

			<tbody>
				{bodyRows.map((row, index) => (
					<tr key={index}>
						{row.map((cell, index) => (
							<td key={index}>{renderChildren(cell[1])}</td>
						))}
					</tr>
				))}
			</tbody>
		</table>
	)
}

const getPassageQuery = db.prepare<
	{ ref: string },
	{ book: string; chapter: string; data: string }
>(
	sql`
    SELECT book.title as book, chapter.ref as chapter, chapter.data
    FROM chapter
    JOIN book ON book.ref = chapter.book_ref
    WHERE chapter.ref = @ref
  `,
)

async function getPassage(ref: string): Promise<Node[]> {
	if (process.env.NODE_ENV === 'development') {
		return TEST_PASSAGE
	}

	const row = getPassageQuery.get({ ref })
	if (!row) {
		notFound()
	}

	const chapterNumber = row.chapter.split('.')[1]

	return [['cl', row.book, chapterNumber], ...JSON.parse(row.data)]
}
